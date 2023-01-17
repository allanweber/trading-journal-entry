package com.trading.journal.entry.entries.trade.aggregate;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.mongodb.BasicDBObject;
import com.trading.journal.entry.entries.EntryRepository;
import com.trading.journal.entry.entries.EntryType;
import com.trading.journal.entry.entries.trade.impl.TradeCollectionName;
import com.trading.journal.entry.queries.CollectionName;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class AggregateService {

    private final EntryRepository repository;
    private final TradeCollectionName tradeCollectionName;

    public List<PeriodAggregatedResult> aggregatePeriod(AccessTokenInfo accessToken, String journalId, AggregateTrade aggregateTrade) {
        CollectionName entriesCollection = tradeCollectionName.collectionName(accessToken, journalId);

        String query = "{ _id: { $dateToString: { format: '" + aggregateTrade.getAggregateType().getGroupBy() + "', date: '$date'} }, " +
                "result:{'$sum': {'$toDouble': '$netResult'}}, count: { $sum: 1 }}";

        AggregationOperation group = aggregationOperationContext -> new Document("$group", BasicDBObject.parse(query));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria("type").is(EntryType.TRADE.name())),
                group,
                Aggregation.sort(Sort.Direction.DESC, "_id"),
                Aggregation.project().andExclude("_id")
                        .and("$_id").as("group")
                        .and(ArithmeticOperators.Round.roundValueOf("$result").place(2)).as("result")
                        .andInclude("count"),
                Aggregation.skip(aggregateTrade.getSkip()),
                Aggregation.limit(aggregateTrade.getSize())
        );
        List<PeriodAggregated> unsortedItems = repository.aggregate(aggregation, entriesCollection, PeriodAggregated.class);

        return unsortedItems.stream()
                .collect(getGroupingBy(aggregateTrade.getAggregateType()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(entry -> new PeriodAggregatedResult(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Collector<PeriodAggregated, ?, Map<String, List<PeriodAggregated>>> getGroupingBy(AggregateType aggregateType) {
        Collector<PeriodAggregated, ?, Map<String, List<PeriodAggregated>>> group;
        if (AggregateType.DAY.equals(aggregateType)) {
            // Group by Month => Day
            // Grab the correspondent Year and Month to group
            group = groupingBy(item -> item.getGroup().substring(0, 7));
        } else if (AggregateType.WEEK.equals(aggregateType)) {
            // Group by Month => Week
            // Grab the Year and Week number, transform in a date and get the Year and Month number to group
            group = groupingBy(item -> {
                String[] groupParts = item.getGroup().split("-");
                int year = Integer.parseInt(groupParts[0]);
                int week = Integer.parseInt(groupParts[1]);
                LocalDate date = LocalDate.ofYearDay(year, 1)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                return year + "-" + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue());
            });
        } else {
            //Group by Year => Month
            group = groupingBy(item -> item.getGroup().substring(0, 4));
        }
        return group;
    }
}

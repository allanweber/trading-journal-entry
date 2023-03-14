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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class AggregateService {

    private final EntryRepository repository;
    private final TradeCollectionName tradeCollectionName;

    public PeriodAggregatedResult aggregatePeriod(AccessTokenInfo accessToken, String journalId, AggregateTrade aggregateTrade) {
        CollectionName entriesCollection = tradeCollectionName.collectionName(accessToken, journalId);

        String groupQuery = "{ _id: { $dateToString: { format: '" + aggregateTrade.getAggregateType().getGroupBy() + "', date: '$date'} }, " +
                "result:{'$sum': {'$toDouble': '$netResult'}}, count: { $sum: 1 }}";

        String facetQuery = "{ result: [{ $skip: " + aggregateTrade.getSkip() + " }, { $limit: " + aggregateTrade.getSize() + " }], totalCount: [ {$count: 'count'}]}";

        AggregationOperation group = aggregationOperationContext -> new Document("$group", BasicDBObject.parse(groupQuery));
        AggregationOperation facet = aggregationOperationContext -> new Document("$facet", BasicDBObject.parse(facetQuery));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria("type").is(EntryType.TRADE.name())),
                group,
                Aggregation.sort(Sort.Direction.DESC, "_id"),
                Aggregation.project().andExclude("_id")
                        .and("$_id").as("group")
                        .and(ArithmeticOperators.Round.roundValueOf("$result").place(2)).as("result")
                        .andInclude("count"),
                facet,
                Aggregation.unwind("$totalCount"),
                Aggregation.project("result").and("$totalCount.count").as("total")
        );

        List<PeriodAggregatedQueryResult> queryResult = repository.aggregate(aggregation, entriesCollection, PeriodAggregatedQueryResult.class);

        return queryResult.stream().findFirst()
                .map(firstItem -> {
                    List<PeriodAggregated> items = firstItem.getResult()
                            .stream()
                            .collect(getGroupingBy(aggregateTrade.getAggregateType()))
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                            .map(entry -> new PeriodAggregated(entry.getKey(), entry.getValue()))
                            .toList();
                    return new PeriodAggregatedResult(items, firstItem.getTotal());
                }).orElse(new PeriodAggregatedResult(emptyList(), 0L));
    }

    public List<TradesAggregated> aggregateTrades(AccessTokenInfo accessToken, String journalId, AggregateTrade aggregateTrade) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime from = LocalDateTime.parse(aggregateTrade.getFrom(), formatter);
        LocalDateTime until = LocalDateTime.parse(aggregateTrade.getUntil(), formatter);

        CollectionName entriesCollection = tradeCollectionName.collectionName(accessToken, journalId);

        String groupQuery = "{ _id: { $dateToString: { format: '%Y-%m-%d', date: '$date'} }, " +
                "items: { $push: {'tradeId':{ $convert: { input: '$_id', to: 'string' } }, 'symbol' : '$symbol', " +
                "order: { $dateToString: { format: '%Y-%m-%d %H:%M:%S', date: '$date'} } , " +
                "'date':'$date', 'exitDate':'$exitDate', 'netResult':'$netResult'} } }";

        AggregationOperation group = aggregationOperationContext -> new Document("$group", BasicDBObject.parse(groupQuery));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria("type").is(EntryType.TRADE.name())),
                Aggregation.match(new Criteria("date").gte(from).lte(until)),
                group,
                Aggregation.unwind("$items"),
                Aggregation.sort(Sort.Direction.DESC, "items.order"),
                Aggregation.group("$_id").push("$items").as("items").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "_id"),

                Aggregation.project().andExclude("_id")
                        .and("$_id").as("group")
                        .andInclude("items", "count")
        );
        return repository.aggregate(aggregation, entriesCollection, TradesAggregated.class);
    }

    private static Collector<PeriodItem, ?, Map<String, List<PeriodItem>>> getGroupingBy(AggregateType aggregateType) {
        Collector<PeriodItem, ?, Map<String, List<PeriodItem>>> group;
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

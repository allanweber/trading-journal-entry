package com.trading.journal.entry.queries;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.util.StringUtils;

@EqualsAndHashCode
public class CollectionName {

    public static final String SEPARATOR = "_";
    private final AccessTokenInfo accessToken;
    private final String middleName;

    public CollectionName(AccessTokenInfo accessToken) {
        this.accessToken = accessToken;
        this.middleName = "";
    }

    public CollectionName(AccessTokenInfo accessToken, String middleName) {
        this.accessToken = accessToken;
        this.middleName = middleName;
    }

    public String collectionName(MongoEntityInformation<?, ?> metadata) {
        StringBuilder builder = new StringBuilder().append(accessToken.tenancyName()).append(SEPARATOR);
        if (StringUtils.hasText(middleName)) {
            builder.append(middleName.replaceAll("\\s+","")).append(SEPARATOR);
        }
        return builder.append(metadata.getCollectionName()).toString();
    }
}

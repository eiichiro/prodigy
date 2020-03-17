package io.eiichiro.prodigy.faults;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.ItemCollectionSizeLimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.RequestLimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TransactionConflictException;

import io.eiichiro.prodigy.Fault;
import io.eiichiro.prodigy.Interceptor;
import io.eiichiro.prodigy.Invocation;
import io.eiichiro.prodigy.Named;
import io.eiichiro.prodigy.Validator;
import io.eiichiro.prodigy.Violation;

@Named("dynamodb-error")
public class AmazonDynamoDBErrorFault extends Fault implements Validator, Interceptor {

    private static final String MESSAGE = "This exception is simulated by Prodigy";

    private static final Map<Integer, Map<String, Class<? extends AmazonServiceException>>> EXCEPTIONS = new HashMap<>();

    static {
        // Common errors
        EXCEPTIONS.put(400, new HashMap<>());
        EXCEPTIONS.get(400).put("AccessDeniedException", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("IncompleteSignature", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("InvalidAction", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("InvalidParameterCombination", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("InvalidParameterValue", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("InvalidQueryParameter", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("MissingAction", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("MissingParameter", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("RequestExpired", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("ThrottlingException", AmazonDynamoDBException.class);
        EXCEPTIONS.get(400).put("ThrottlingException", AmazonDynamoDBException.class);
        EXCEPTIONS.put(403, new HashMap<>());
        EXCEPTIONS.get(403).put("InvalidClientTokenId", AmazonDynamoDBException.class);
        EXCEPTIONS.get(403).put("MissingAuthenticationToken", AmazonDynamoDBException.class);
        EXCEPTIONS.get(403).put("OptInRequired", AmazonDynamoDBException.class);
        EXCEPTIONS.put(404, new HashMap<>());
        EXCEPTIONS.get(404).put("MalformedQueryString", AmazonDynamoDBException.class);
        EXCEPTIONS.put(500, new HashMap<>());
        EXCEPTIONS.get(500).put("InternalFailure", AmazonDynamoDBException.class);
        EXCEPTIONS.put(503, new HashMap<>());
        EXCEPTIONS.get(503).put("ServiceUnavailable", AmazonDynamoDBException.class);

        // DynamoDB errors
        EXCEPTIONS.get(500).put("InternalServerError", InternalServerErrorException.class);
        EXCEPTIONS.get(400).put("ProvisionedThroughputExceededException", ProvisionedThroughputExceededException.class);
        EXCEPTIONS.get(400).put("RequestLimitExceeded", RequestLimitExceededException.class);
        EXCEPTIONS.get(400).put("ResourceNotFoundException", ResourceNotFoundException.class);
        EXCEPTIONS.get(400).put("ItemCollectionSizeLimitExceededException", ItemCollectionSizeLimitExceededException.class);
        EXCEPTIONS.get(400).put("ConditionalCheckFailedException", ConditionalCheckFailedException.class);
        EXCEPTIONS.get(400).put("TransactionConflictException", TransactionConflictException.class);
    }

    private Integer statusCode;

    private String errorCode;

    @Override
    public Set<Violation> validate() {
        final Set<Violation> violations = new HashSet<>();

        if (statusCode == null || (statusCode != null && !EXCEPTIONS.containsKey(statusCode))) {
            violations.add(new Violation("Parameter 'statusCode' is any one of [" + EXCEPTIONS.keySet() + "]"));
            return violations;
        }

        final Map<String, Class<? extends AmazonServiceException>> map = EXCEPTIONS.get(statusCode);

        if ((errorCode == null && map.size() != 1) || (errorCode != null && !map.containsKey(errorCode))) {
            violations.add(new Violation("Parameter 'errorCode' is any one of [" + map.keySet() + "]"));
        }

        return violations;
    }

    /**
     * @return the statusCode
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(final Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean intercept(final Invocation invocation) throws Throwable {
        if (!(invocation.target() instanceof AmazonDynamoDB)) {
            return false;
        }

        final Map<String, Class<? extends AmazonServiceException>> map = EXCEPTIONS.get(statusCode);
        String errorCode = (this.errorCode == null) ? map.keySet().iterator().next() : this.errorCode;
        final Class<? extends AmazonServiceException> clazz = map.get(errorCode);
        final Constructor<? extends AmazonServiceException> constructor = clazz.getConstructor(String.class);
        final AmazonServiceException exception = constructor.newInstance(MESSAGE);
        exception.setStatusCode(statusCode);
        exception.setErrorCode(errorCode);
        exception.setServiceName("AmazonDynamoDBv2");
        invocation.throwable(exception);
        return true;
    }

}

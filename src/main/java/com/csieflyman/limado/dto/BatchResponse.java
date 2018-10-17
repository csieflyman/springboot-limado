package com.csieflyman.limado.dto;

import com.csieflyman.limado.exception.BadBatchRequestException;
import com.csieflyman.limado.exception.BaseException;
import com.csieflyman.limado.model.Identifiable;
import com.csieflyman.limado.util.converter.json.JsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author James Lin
 */
public class BatchResponse extends Response<BatchResponse.BatchResult> {

    public static BatchResponse empty() {
        return new BatchResponse(new ArrayList<>(), LinkedHashMultimap.create());
    }

    public BatchResponse(List<String> successIds, LinkedHashMultimap<String, Throwable> failureResultMap) {
        super(failureResultMap != null && failureResultMap.size() > 0 ?
                ResponseCode.REQUEST_BATCH_FAILURE : ResponseCode.SUCCESS, new BatchResult(successIds, failureResultMap));
    }

    public BatchResponse(Map<String, Object> successResultMap, LinkedHashMultimap<String, Throwable> failureResultMap) {
        super(failureResultMap != null && failureResultMap.size() > 0 ?
                ResponseCode.REQUEST_BATCH_FAILURE : ResponseCode.SUCCESS, new BatchResult(successResultMap, failureResultMap));
    }

    private BatchResponse(List<String> successIds, BadBatchRequestException e) {
        super(ResponseCode.REQUEST_BATCH_FAILURE, new BatchResult(successIds, e));
    }

    public void addFailure(BadBatchRequestException e) {
        for(Identifiable Identifiable: e.getInvalidObjects()) {
            FailureResult failureResult = new FailureResult(Identifiable.getId().toString());
            failureResult = getFailureResult(failureResult);
            failureResult.addError(e.getResponseCode(), e.getErrorMsg(Identifiable));
        }
    }

    public void addFailure(String id, Throwable e) {
        FailureResult failureResult = new FailureResult(id);
        failureResult = getFailureResult(failureResult);
        failureResult.addError(e);
    }

    private FailureResult getFailureResult(FailureResult failureResult) {
        int index = this.result.failure.indexOf(failureResult);
        if(index != -1) {
            failureResult = this.result.failure.get(index);
        }
        else {
            result.failure.add(failureResult);
        }
        return failureResult;
    }

    public void add(BatchResponse response) {
        this.result.success.addAll(response.result.success);
        this.result.failure.addAll(response.result.failure);
    }

    public boolean isFailureObject(String id) {
        return result.failure.stream().anyMatch(failureResult -> failureResult.id.equals(id));
    }

    @JsonIgnore
    public boolean hasFailure() {
        return !result.failure.isEmpty();
    }

    //可參考 facebook Graph API範例 https://developers.facebook.com/docs/graph-api/making-multiple-requests/
    @JsonIgnore
    public int getStatusCode() {
        if(result.success.size() + result.failure.size() == 1) {
            if(hasFailure()) {
                return result.failure.iterator().next().statusCode;
            }
            else {
                return 200;
            }
        }
        else {
            return 200;
        }
    }

    static class BatchResult {

        public List<SuccessResult> success = new ArrayList<>();
        public List<FailureResult> failure = new ArrayList<>();

        private BatchResult(List<String> successIds, BadBatchRequestException e) {
            if(successIds != null) {
                this.success = successIds.stream().map(SuccessResult::new).collect(Collectors.toList());
            }
            for(com.csieflyman.limado.model.Identifiable Identifiable: e.getInvalidObjects()) {
                FailureResult failureResult = new FailureResult(Identifiable.getId().toString());
                failureResult.addError(e.getResponseCode(), e.getErrorMsg(Identifiable));
                failure.add(failureResult);
            }
        }

        private BatchResult(List<String> successIds, LinkedHashMultimap<String, Throwable> failureResultMap) {
            this(successIds.stream().collect(Collectors.toMap(id -> id, id -> JsonConverter.newObject())), failureResultMap);
        }

        private BatchResult(Map<String, Object> successResultMap, LinkedHashMultimap<String, Throwable> failureResultMap) {
            if(successResultMap != null) {
                this.success = successResultMap.entrySet().stream().map(entry ->
                        new SuccessResult(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            }
            if(failureResultMap != null) {
                for (String id : failureResultMap.keySet()) {
                    FailureResult result = new FailureResult(id);
                    failureResultMap.get(id).forEach(result::addError);
                    failure.add(result);
                }
            }
        }
    }

    private static class SuccessResult{
        public String id;
        public Object data;

        private SuccessResult(String id) {
            this.id = id;
        }

        private SuccessResult(String id, Object data) {
            this.id = id;
            this.data = data;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(id).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            FailureResult other = (FailureResult) obj;
            return new EqualsBuilder().append(id, other.id).isEquals();
        }
    }

    private static class FailureResult {

        public String id;
        public List<FailureCodeMessage> errors = new ArrayList<>();
        @JsonIgnore
        private Integer statusCode;

        private FailureResult(String id) {
            this.id = id;
        }

        private void addError(Throwable e) {
            ResponseCode responseCode = e instanceof BaseException ? ((BaseException) e).getResponseCode() : ResponseCode.INTERNAL_SERVER_ERROR;
            String message = responseCode.getMessage() + " " + (e instanceof BaseException ? e.getMessage() : Throwables.getRootCause(e).getMessage());
            errors.add(new FailureCodeMessage(responseCode.getCode(), message));
            if(statusCode == null) {
                statusCode = responseCode.getStatusCode();
            }
        }

        private void addError(ResponseCode responseCode, String message) {
            errors.add(new FailureCodeMessage(responseCode.getCode(), message));
            if(statusCode == null) {
                statusCode = responseCode.getStatusCode();
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(id).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            FailureResult other = (FailureResult) obj;
            return new EqualsBuilder().append(id, other.id).isEquals();
        }
    }

    private static class FailureCodeMessage {
        public String code;
        public String message;

        private FailureCodeMessage(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}

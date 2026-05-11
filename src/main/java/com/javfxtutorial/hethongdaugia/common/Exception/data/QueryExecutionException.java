package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Lỗi thực thi query
 */
public class QueryExecutionException extends DataException {
    private static final long serialVersionUID = 1L;
    private final String query;
    public QueryExecutionException(String query){
        super(ErrorCode.DATA_QUERY_FAILED,
            String.format("Lỗi thực thi truy vấn: %s", query));
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}

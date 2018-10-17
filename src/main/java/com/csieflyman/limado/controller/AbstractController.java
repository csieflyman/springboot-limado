package com.csieflyman.limado.controller;

import com.csieflyman.limado.dto.PagingQueryResponse;
import com.csieflyman.limado.exception.BadRequestException;
import com.csieflyman.limado.model.BaseModel;
import com.csieflyman.limado.service.GenericService;
import com.csieflyman.limado.util.query.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author James Lin
 */
public class AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(AbstractController.class);

    @Autowired
    protected HttpServletRequest request;

    protected void processBindingResult(BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException("invalid data.", null, result.getFieldErrors().stream()
                    .map(error -> error.getField() + " : " + error.getDefaultMessage()).collect(Collectors.joining(";")));
        }
    }

    protected <T extends BaseModel<ID>, ID> ResponseEntity findEntities(GenericService<T, ID> genericService) {
        return findEntities(genericService, null);
    }

    protected <T extends BaseModel<ID>, ID> ResponseEntity findEntities(GenericService<T, ID> genericService, QueryParams params) {
        return findEntities(genericService::findSize, genericService::find, params);
    }

    protected <T extends BaseModel> ResponseEntity findEntities(Function<QueryParams, Integer> findSizeFunction, Function<QueryParams, List<T>> findFunction, QueryParams params) {
        if(params == null) {
            params = QueryParams.create(request.getParameterMap());
        }

        if (params.isOnlySize()) {
            return ResponseEntity.ok(String.valueOf(findSizeFunction.apply(params)));
        }
        else {
            List<T> entities = findFunction.apply(params);
            if(params.isPagingQuery()) {
                int total = findSizeFunction.apply(params);
                return ResponseEntity.ok(new PagingQueryResponse<>(total, params.getPageSize(), params.getPageNo(), entities));
            }
            else {
                return ResponseEntity.ok(entities);
            }
        }
    }
}
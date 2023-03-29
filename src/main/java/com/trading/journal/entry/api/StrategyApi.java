package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.strategy.Strategy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Strategy Api")
@RequestMapping("/strategies")
public interface StrategyApi {

    @ApiOperation(notes = "Get all Strategies", value = "Get all Strategies")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Strategies Returned")
    })
    @GetMapping()
    ResponseEntity<PageWrapper<Strategy>> getAll(
            AccessTokenInfo accessTokenInfo,
            @SortDefault.SortDefaults({@SortDefault(sort = "name", direction = Sort.Direction.ASC)}) Pageable pageable
    );

    @ApiOperation(notes = "Create or Update Strategy", value = "Create or Update Strategy")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Strategy Created"),
            @ApiResponse(code = 200, message = "Strategy Updated")
    })
    @PostMapping()
    ResponseEntity<Strategy> save(AccessTokenInfo accessTokenInfo, @RequestBody @Valid Strategy strategy);

    @ApiOperation(notes = "Get Strategy by Id", value = "Get Strategy by Id")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Strategy Returned")
    })
    @GetMapping("/{strategy-id}")
    ResponseEntity<Strategy> getById(AccessTokenInfo accessTokenInfo, @PathVariable(name = "strategy-id") String strategyId);

    @ApiOperation(notes = "Delete Strategy by Id", value = "Delete Strategy by Id")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Strategy Deleted")
    })
    @DeleteMapping("/{strategy-id}")
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, @PathVariable(name = "strategy-id") String strategyId);
}

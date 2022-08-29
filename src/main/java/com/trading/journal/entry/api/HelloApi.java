package com.trading.journal.entry.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.entry.hello.Hello;
import com.trading.journal.entry.pageable.PageResponse;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Hello Api")
@RequestMapping("/hello")
public interface HelloApi {

    @ApiOperation(notes = "Get all", value = "Get all")
    @ApiResponses(@ApiResponse(code = 200, message = "Records retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<Hello>> getAll(
            AccessTokenInfo accessTokenInfo,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @ApiParam(name = "sort", value = "A array with property and direction such as \"id,asc\", \"name,desc\"")
            @RequestParam(value = "sort", required = false) String[] sort);

    @ApiOperation(notes = "Create new", value = "Create new")
    @ApiResponses(@ApiResponse(code = 201, message = "Record created"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Hello> create(AccessTokenInfo accessTokenInfo, @RequestBody @Valid Hello data);

    @ApiOperation(notes = "Get by id", value = "Get record by its id")
    @ApiResponses(@ApiResponse(code = 200, message = "Record retrieved"))
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Hello> getById(AccessTokenInfo accessTokenInfo, @PathVariable String id);
}

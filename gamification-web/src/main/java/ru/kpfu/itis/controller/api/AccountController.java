package ru.kpfu.itis.controller.api;

import com.wordnik.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.dto.AccountProfileDto;
import ru.kpfu.itis.dto.ErrorDto;
import ru.kpfu.itis.dto.enums.Error;
import ru.kpfu.itis.security.SecurityService;
import ru.kpfu.itis.service.AccountService;
import ru.kpfu.itis.util.Constant;

/**
 * Created by Rigen on 22.06.15.
 */
@Api(value = "user", description = "operation with users")
@RequestMapping(Constant.API_URI_PREFIX + "/user")
@RestController("apiAccountController")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private SecurityService securityService;

    @ApiOperation(httpMethod = "GET", value = "get user's profile")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "string", paramType = "query")})
    @ResponseBody
    public ResponseEntity<? super AccountProfileDto> getProfile(@PathVariable Long id) {
        AccountProfileDto userProfile = accountService.getUserProfile(id);
        if (userProfile == null) {
            return new ResponseEntity<>(new ErrorDto(Error.USER_NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userProfile, HttpStatus.OK);
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "string", paramType = "query")})
    @ResponseBody
    public ResponseEntity<? super AccountProfileDto> getMyProfile() {
        return getProfile(securityService.getCurrentUserId());
    }


}

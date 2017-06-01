package beaver.backend.controller;

import beaver.backend.entity.responseType.Info;
import beaver.backend.entity.responseType.UserDetail;
import beaver.backend.exception.DuplicatedUserName;
import beaver.backend.exception.NotLogin;
import beaver.backend.repository.UserRepository;
import beaver.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by parda on 2017/6/2.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/get-user")
    public ResponseEntity<Info> getDetail(HttpSession session) throws NotLogin, Exception {
        if (session.getAttribute("currentUser") == null)
            throw new NotLogin();
        return new ResponseEntity<Info>(new Info("success", "Get User Info", userService.getUserDetail((long)session.getAttribute("currentUser"))),HttpStatus.OK);
    }

    @PostMapping("/update-user")
    public ResponseEntity<Info> updateUser(@RequestBody UserDetail userDetail, HttpSession session) throws NotLogin, DuplicatedUserName, Exception {
        if (session.getAttribute("currentUser") == null)
            throw new NotLogin();
        if (userDetail.getUsername() != null && userDetail.getUsername() != userService.getName((long)session.getAttribute("currentUser")) && userService.checkNameExist(userDetail.getUsername()))
            throw new DuplicatedUserName();
        System.out.println(userDetail);
        userService.update((long)session.getAttribute("currentUser"), userDetail);
        return new ResponseEntity<Info>(new Info("success", "Update User Success"), HttpStatus.OK);
    }
}

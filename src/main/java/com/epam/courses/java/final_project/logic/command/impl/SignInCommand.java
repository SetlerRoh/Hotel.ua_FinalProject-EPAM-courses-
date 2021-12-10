package com.epam.courses.java.final_project.logic.command.impl;

import com.epam.courses.java.final_project.dao.impl.jdbc.JDBCException;
import com.epam.courses.java.final_project.logic.command.Command;
import com.epam.courses.java.final_project.logic.command.Response;
import com.epam.courses.java.final_project.model.User;
import com.epam.courses.java.final_project.service.UserService;
import com.epam.courses.java.final_project.util.MailManager;
import com.epam.courses.java.final_project.util.PasswordCryptoPbkdf2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.epam.courses.java.final_project.util.constant.CommandConstant.*;
import static com.epam.courses.java.final_project.util.constant.Constant.LOG_TRACE;
import static com.epam.courses.java.final_project.util.constant.Constant.PARAM_ID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class SignInCommand implements Command {

    private static final Logger log = LogManager.getLogger(SignInCommand.class);

    @Override
    public Response execute(HttpServletRequest req, HttpServletResponse resp) throws JDBCException {
        String errorMsg = "Incorrect email or password";
        String email = req.getParameter(PARAM_EMAIL);
        String password = req.getParameter(PARAM_PWD);

        if (req.getParameter(PARAM_ID) != null) {
            return new Response(Response.Direction.Redirect, INDEX_JSP);
        }

        log.error("error");
        Optional<User> user = UserService.getByEmail(email);
        if (user.isEmpty()) {
            req.getSession().setAttribute(ATTRIBUTE_EMAIL, email);
            req.getSession().setAttribute(ATTRIBUTE_LOGIN_EX, errorMsg);
            return new Response(Response.Direction.Redirect, INDEX_JSP);
        } else if (!PasswordCryptoPbkdf2.validatePwd(password, user.get().getPassword())) {
            req.getSession().setAttribute(ATTRIBUTE_EMAIL, email);
            req.getSession().setAttribute(ATTRIBUTE_LOGIN_EX, errorMsg);
            return new Response(Response.Direction.Redirect, INDEX_JSP);
        } else if (user.get().getVerification() != null) {
            req.getSession().setAttribute(ATTRIBUTE_EMAIL, email);
            req.getSession().setAttribute(ATTRIBUTE_LOGIN_EX, "Please verify your email first");
            MailManager.getInstance().sendEmailVerification(email, user.get().getName(),
                    user.get().getSurname(), user.get().getVerification());
            return new Response(Response.Direction.Redirect, INDEX_JSP);
        }

        req.getSession().setAttribute(ATTRIBUTE_ID, user.get().getId());
        req.getSession().setAttribute(ATTRIBUTE_USER, user.get());
        req.getSession().setAttribute(ATTRIBUTE_ROLE, user.get().getRole().name());
        return new Response(Response.Direction.Redirect, INDEX_ACT);
    }

    @Override
    public String getCommand() {
        return SIGN_IN;
    }
}

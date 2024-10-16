package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Comment;
import repository.implementation.CommentRepositoryImpl;
import repository.interfaces.CommentRepository;
import service.CommentService;
import util.JsonBodyObj;

public class CommentServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CommentServlet.class);

    private CommentRepository commentRepository = new CommentRepositoryImpl();
    private CommentService commentService = new CommentService(commentRepository);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JsonBodyObj requestBody = new JsonBodyObj(req);

        logger.info("request body : " + requestBody.getRequestBody());

        String commentIdParam = requestBody.getParameter("commentId");
        String content = requestBody.getParameter("content");

        logger.info("comment id : " + commentIdParam);
        logger.info("content : " + content);

        int commentId = Integer.parseInt(commentIdParam);

        Comment comment = commentService.getComment(commentId).orElse(null);

        comment.setContent(content);

        try {
            commentService.updateComment(comment);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\": \"Comment updated successfully\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"unknown error occured\"}");
        }
    }

}

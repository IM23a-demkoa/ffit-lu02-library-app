package ch.bzz;

import ch.bzz.db.BookPersistor;
import ch.bzz.db.UserPersistor;
import ch.bzz.model.Book;
import ch.bzz.model.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.javalin.plugin.json.JavalinJackson;
import io.javalin.core.validation.BodyValidator;
import io.javalin.core.validation.Validator;

import java.util.Base64;
import java.util.List;
import java.util.Map;



public class JavalinMain {

    private static final Logger log = LoggerFactory.getLogger(JavalinMain.class);

    public static void main(String[] args) {
        BookPersistor bookPersistor = new BookPersistor();
        UserPersistor userPersistor = new UserPersistor();

        // Javalin starten
        Javalin app = Javalin.create(config -> {
            config.defaultContentType("application/json");
        }).start(7070);

        log.info("Javalin Server gestartet auf Port 7070");

        // GET /books?limit=10
        app.get("/books", ctx -> {
            String limitParam = ctx.queryParam("limit");
            int limit = -1;
            if (limitParam != null) {
                try {
                    limit = Integer.parseInt(limitParam);
                } catch (NumberFormatException e) {
                    log.warn("Ungültiges Limit in QueryParam: '{}'", limitParam, e);
                }
            }
            List<Book> books = bookPersistor.getAll(limit);
            ctx.json(books);
        });

        // POST /auth/login
        app.post("/auth/login", ctx -> {
            var json = ctx.bodyValidator(Map.class)
                    .check(m -> m.containsKey("email"), "email is required")
                    .check(m -> m.containsKey("password"), "password is required")
                    .get();

            String inputEmail = (String) json.get("email");
            String inputPassword = (String) json.get("password");

            User user = userPersistor.findByEmail(inputEmail);

            if (user != null) {
                byte[] storedSalt = Base64.getDecoder().decode(user.getPasswordSalt());
                byte[] storedHash = Base64.getDecoder().decode(user.getPasswordHash());

                if (PasswordHandler.verifyPassword(inputPassword, storedHash, storedSalt)) {
                    String jwt = JwtHandler.createJwt(inputEmail, user.getId());
                    ctx.json(Map.of("token", jwt));
                    return;
                }
            }
            ctx.status(401).json(Map.of("error", "Invalid email or password"));
        });

        // PUT /auth/change-password
        app.put("/auth/change-password", ctx -> {
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).json(Map.of("error", "Authorization header missing or invalid"));
                return;
            }

            String token = authHeader.substring("Bearer ".length());
            var claims = JwtHandler.parseJwt(token); // implementiere parseJwt in JwtHandler
            Integer userId = claims.get("userId", Integer.class);

            var json = ctx.bodyValidator(Map.class)
                    .check(m -> m.containsKey("oldPassword"), "oldPassword is required")
                    .check(m -> m.containsKey("newPassword"), "newPassword is required")
                    .get();

            String oldPassword = (String) json.get("oldPassword");
            String newPassword = (String) json.get("newPassword");

            User user = userPersistor.findById(userId);
            if (user != null) {
                byte[] storedSalt = Base64.getDecoder().decode(user.getPasswordSalt());
                byte[] storedHash = Base64.getDecoder().decode(user.getPasswordHash());

                if (PasswordHandler.verifyPassword(oldPassword, storedHash, storedSalt)) {
                    byte[] newHash = PasswordHandler.hashPassword(newPassword, storedSalt);
                    user.setPasswordHash(Base64.getEncoder().encodeToString(newHash));
                    userPersistor.save(user);
                    ctx.json(Map.of("message", "Password changed successfully"));
                    return;
                }
            }
            ctx.status(401).json(Map.of("error", "Invalid old password"));
        });
    }
}

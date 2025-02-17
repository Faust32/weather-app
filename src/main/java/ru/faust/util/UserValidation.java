package ru.faust.util;

import lombok.experimental.UtilityClass;
import ru.faust.exception.InvalidPasswordException;
import ru.faust.exception.InvalidUsernameException;
import ru.faust.exception.UtilityFilesLoadingException;
import ru.faust.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@UtilityClass
public class UserValidation {

    private static final int MIN_LOGIN_LENGTH = 3;

    private static final int MAX_LOGIN_LENGTH = 20;

    private static final int MIN_PASSWORD_LENGTH = 6;

    private static final int MAX_PASSWORD_LENGTH = 32;

    private static final Pattern loginPatter = Pattern.compile("^[a-zA-Z0-9]+$");

    private static final Set<String> BAD_WORDS = new HashSet<>();

    static {
        try {
            loadDictionary();
        } catch (IOException e) {
            throw new UtilityFilesLoadingException("An error occurred while trying to load profanity dictionary. Please contact the developer.");
        }
    }

    private void loadDictionary() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(UserValidation.class.getClassLoader().getResourceAsStream("profanity/dictionary.en"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                BAD_WORDS.add(line.toLowerCase());
            }
        }
    }

    public void validateUser(User user) {
        isUsernameValid(user.getUsername());
        isPasswordValid(user.getPassword());
    }

    private void isUsernameValid(String login) {
        if (login == null || login.isEmpty()) {
            throw new InvalidUsernameException("Username cannot be empty.");
        }
        if (login.length() < MIN_LOGIN_LENGTH || login.length() > MAX_LOGIN_LENGTH) {
            throw new InvalidUsernameException("Username must be between " + MIN_LOGIN_LENGTH + " and " + MAX_LOGIN_LENGTH
                    + " characters long.");
        }
        if (!loginPatter.matcher(login).matches()) {
            throw new InvalidUsernameException("Username contains invalid characters: it must not contain" +
                    " any other characters except latin letters and numbers");
        }
        String maybeBadWord = containsBadWords(login);
        if (!maybeBadWord.isEmpty()) {
            throw new InvalidUsernameException("Username contains prohibited words: " + maybeBadWord);
        }
    }

    private String containsBadWords(String name) {
        String lowerCaseName = name.toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (lowerCaseName.equals(badWord) || lowerCaseName.contains(badWord)) {
                return badWord;
            }
        }
        return "";
    }

    private void isPasswordValid(String password) {
        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException("Password cannot be empty.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH
                    + " characters long.");
        }
    }
}

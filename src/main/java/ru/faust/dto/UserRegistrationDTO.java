package ru.faust.dto;

import ru.faust.model.User;

public record UserRegistrationDTO(User user,
                                  String repeatPassword) {
}

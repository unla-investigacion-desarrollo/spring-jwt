package com.project.auth.services;

import com.project.auth.models.database.Users;

import java.util.List;

public interface IMailService {

    void sendRecoveryPassEmail(List<String> emailsTo, String username,
                               String temporaryPassword, Users user);

    void sendNewUserEmail(List<String> emailsTo, String username,
                          String temporaryPassword, Users user);
}

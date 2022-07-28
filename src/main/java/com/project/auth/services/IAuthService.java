package com.project.auth.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.auth.dtos.request.login.RequestLoginDTO;
import com.project.auth.dtos.request.login.RequestRecoverPassDTO;
import com.project.auth.dtos.response.login.ResponseLoginDTO;

public interface IAuthService {

    ResponseLoginDTO signInService(RequestLoginDTO request) throws JsonProcessingException;

    String recoverPassword(RequestRecoverPassDTO request);
}

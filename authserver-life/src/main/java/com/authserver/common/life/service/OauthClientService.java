package com.authserver.common.life.service;

import com.authserver.common.life.entity.OauthClient;

/**
 * oauth客户端表
 *
 * @author code@code.com
 * @date 2022-02-21 20:21:01
 */
public interface OauthClientService {


    OauthClient selectClientByClientId(String id);

}
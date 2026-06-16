package com.djt.jukeanator_engine.domain.user.service;

import com.djt.jukeanator_engine.domain.user.dto.AuthResponse;
import com.djt.jukeanator_engine.domain.user.dto.LoginRequest;
import com.djt.jukeanator_engine.domain.user.dto.RegisterRequest;
import com.djt.jukeanator_engine.domain.user.dto.UserProfileDto;

/**
 * @author tmyers
 */
public interface UserService {

  /**
   * 
   * @param request
   * @return
   */
  AuthResponse register(RegisterRequest request);
  
  /**
   * 
   * @param request
   * @return
   */
  AuthResponse login(LoginRequest request);

  /**
   * 
   * @param emailAddress
   * @return
   */
  UserProfileDto getProfile(String emailAddress);
}

package org.gs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.gs.dto.UserRequest;
import org.gs.exception.UserNotFoundException;
import org.gs.model.User;
import org.gs.repository.UserRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;
    private Map<String, User> userStore = new ConcurrentHashMap<>();

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

  
   public  User addUser(UserRequest user){
    return userRepository.addUser(user);
   }
   
   public User getUserA() {
        return (User) userRepository.getAllUsers();
    }



    public User registerUser(User user) {
        userStore.put(user.getId(), user);
        return user;
    }

    public User getUser(String id) {
        if (!userStore.containsKey(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        return userStore.get(id);
    }

    public Map<String, User> getAllUsers() {
        return userStore;
    }

    public void deleteUser(String id) {
        if (!userStore.containsKey(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        userStore.remove(id);
    }

    public User updateUser(String id, User user) {
        if (!userStore.containsKey(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        userStore.put(id, user);
        return user;
    }

}
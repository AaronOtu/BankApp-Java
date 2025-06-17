package org.gs.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.gs.service.UserService;
import org.gs.dto.UserRequest;
import org.gs.model.User;
import org.gs.repository.UserRepository;

@Path("/users")
public class UserResources {

    private final UserService userService;
    private final UserRepository userRepository;

    @Inject
    public UserResources(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
        User created = userService.registerUser(user);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }*/
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserRequest user) {
        try{

            User created = userRepository.addUser(user);
            return Response.status(Response.Status.CREATED).entity(created).build();
        }catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response getAllUsers() {
    //     return Response.ok(userService.getAllUsers()).build();
    // }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        return Response.ok(userRepository.getAllUsers()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(String id) {
        User user = userService.getUser(id);
        return Response.ok(user).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(String id, User user) {
        User updatedUser = userService.updateUser(id, user);
        return Response.ok(updatedUser).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(String id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }

}

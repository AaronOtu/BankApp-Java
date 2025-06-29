package org.gs.controller;

import org.gs.dto.AccountRequest;
import org.gs.dto.AccountResponse;
import org.gs.repository.AccountRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/accounts")
public class AccountResource {
    /*private final AccountService accountService;*/
    private final AccountRepository accountRepository;

    @Inject
    public AccountResource(/*AccountService accountService, */AccountRepository accountRepository) {
       /*  this.accountService = accountService;*/
        this.accountRepository = accountRepository;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(AccountRequest account) {
        try {
            AccountResponse created = accountRepository.createAccount(account);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(String userId) {
        try {
            AccountResponse account = accountRepository.getAccount(userId);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAccounts() {
        try {
            return Response.ok(accountRepository.getAllAccounts()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(String userId) {
        try {
            accountRepository.deleteAccount(userId);
            return Response.ok("Successfully deleted account").build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}

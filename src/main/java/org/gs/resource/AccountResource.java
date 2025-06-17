package org.gs.resource;

import org.gs.dto.AccountRequest;
import org.gs.dto.BalanceResponse;
import org.gs.model.SavingsAccount;
import org.gs.service.AccountService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/accounts")
public class AccountResource {
    private final AccountService accountService;

    @Inject
    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(AccountRequest account) {
        try {
            SavingsAccount created = accountService.createAccount(account);
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
            SavingsAccount account = accountService.getAccount(userId);
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
            return Response.ok(accountService.getAllAccounts()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // @GET
    // @Path("/balance/{userId}")
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response getBalance(@PathParam{"accountId"} String userId) {
    //     try {
    //         SavingsAccount account = accountService.getBalance(userId);
    //         return Response.ok(account).build();
    //     } catch (Exception e) {
    //         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    //     }
    // }
    @GET
@Path("/{accountId}/balance")
@Produces(MediaType.APPLICATION_JSON)
public Response getBalance(@PathParam("accountId") String accountId) {
    try {
        SavingsAccount account = accountService.getAccount(accountId);
        String userName = account.getFirstName() + " " + account.getLastName();
        BalanceResponse response = new BalanceResponse(account.getBalance(), userName);
        return Response.ok(response).build();
    } catch (IllegalArgumentException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
}

    @DELETE
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(String userId) {
        try {
            accountService.deleteAccount(userId);
            return Response.noContent().build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}

package org.gs.resource;

import org.gs.dto.DepositRequest;
import org.gs.model.SavingsAccount;
import org.gs.service.AccountService;
import org.gs.service.BankOperationService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/accounts")
public class BankOperationResource {
    private BankOperationService bankOperationService;
    private AccountService accountService;

    public BankOperationResource(BankOperationService bankOperationService, AccountService accountService) {
        this.bankOperationService = bankOperationService;
        this.accountService = accountService;
    }

    @Path("/deposit/{userId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deposit(@PathParam("userId") String userId, DepositRequest request) {
        try {
            bankOperationService.deposit(userId, request.getAmount());
            SavingsAccount accountDetails = accountService.getAccount(userId);
            return Response.ok(accountDetails).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}

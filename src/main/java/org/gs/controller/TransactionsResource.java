package org.gs.controller;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.List;

import org.gs.dto.DepositRequest;
import org.gs.dto.TransferRequest;
import org.gs.model.Transactions;
import org.gs.repository.TransactionRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@Path("/transactions")
public class TransactionsResource {
    private final TransactionRepository transactionRepository;

    @Inject
    public TransactionsResource(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deposit(DepositRequest request) {
        try {
            return Response.ok(transactionRepository.deposit(request)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response withdraw(DepositRequest request) {
        try {
            return Response.ok(transactionRepository.withdraw(request)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransactions(@PathParam("accountId") String accountId) {
        try {
            List<Transactions> ts = transactionRepository.getTransactionsByAccount(accountId);
            return Response.ok(ts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(TransferRequest request) {
        try {
            return Response.ok(transactionRepository.transfer(request)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/get-balance/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBalance(@PathParam("accountId") String accountId) {
        try {
            return Response.ok(transactionRepository.getBalance(accountId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @GET
    @Path("/{userId}/csv")
    @Produces("text/csv")
    public Response getTransactionsCsv(@PathParam("userId") String userId) {
        List<Transactions> transactions = transactionRepository.getTransactionsByAccount(userId);

        StreamingOutput stream = output -> {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(String.format("TransactionId,AccountId,Amount,Type,Date%n"));

            for (Transactions tx : transactions) {
                writer.write(String.format("%s,%s,%.2f,%s,%s%n",
                        tx.getId(), tx.getAccountId(),
                        tx.getAmount(), tx.getTransactionType(), tx.getTransactionDate()));
            }
            writer.flush();
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"transactions.csv\"")
                .build();
    }

    /*
     * @GET
     * 
     * @Path("/{userId}/pdf")
     * 
     * @Produces("application/pdf")
     * public Response getTransactionsPdf(@PathParam("userId") String userId) {
     * List<Transactions> transactions =
     * transactionService.getTransactionsByUserId(userId);
     * 
     * ByteArrayOutputStream out = new ByteArrayOutputStream();
     * 
     * // Example using iText 7
     * PdfWriter writer = new PdfWriter(out);
     * PdfDocument pdfDoc = new PdfDocument(writer);
     * Document document = new Document(pdfDoc);
     * 
     * document.add(new Paragraph("Transactions Report"));
     * for (Transactions tx : transactions) {
     * document.add(new Paragraph(String.format(
     * "ID: %s, Account: %s, Amount: %.2f, Type: %s, Date: %s",
     * tx.getTransactionId(), tx.getAccountId(),
     * tx.getAmount(), tx.getType(), tx.getDate())));
     * }
     * 
     * document.close();
     * 
     * return Response.ok(new ByteArrayInputStream(out.toByteArray()))
     * .header("Content-Disposition", "attachment; filename=\"transactions.pdf\"")
     * .build();
     * }
     */

}

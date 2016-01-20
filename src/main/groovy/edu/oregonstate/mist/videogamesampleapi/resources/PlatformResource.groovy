package edu.oregonstate.mist.videogamesampleapi.resources

import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.Error

/**
 * Resource for the Platform object
 */

import edu.oregonstate.mist.videogamesampleapi.core.Platform
import edu.oregonstate.mist.videogamesampleapi.db.PlatformDAO
import edu.oregonstate.mist.api.Resource
import io.dropwizard.auth.Auth

import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Response.ResponseBuilder
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException

@Path("/platforms")
@Produces(MediaType.APPLICATION_JSON)
class PlatformResource extends Resource {

    private final PlatformDAO platformDAO

    public PlatformResource(PlatformDAO platformDAO) {
        this.platformDAO = platformDAO
    }

    /**
     * GETs all of the platforms that are not filtered out by the parameters
     * If no parameters are provided it returns all of the entries
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Platform> getPlatforms(
            @Auth AuthenticatedUser authenticatedUser,
            @QueryParam("releaseYear") String releaseYear,
            @QueryParam("computer") Boolean computer,
            @QueryParam("console") Boolean console) {
        platformDAO.getPlatforms(releaseYear, computer, console)
    }

    /**
     * Returns a single entry based on the id in the path
     */
    @Path("/{id : \\d+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response platformById(@PathParam("id") Integer id) {
        Platform returnPlatform = platformDAO.getPlatformById(id)
        ResponseBuilder responseBuilder

        if(returnPlatform == null) {
           responseBuilder = notFound()
        }
        else {
            responseBuilder = ok(returnPlatform)
        }
        responseBuilder.build()
    }

    /**
     * POSTs a new game to the database
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postPlatform(@Valid Platform newPlatform) {
        ResponseBuilder responseBuilder
        responseBuilder = postHelper(newPlatform)
        responseBuilder.build()
    }

    @Path("/{id : \\d+}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePlatform(@PathParam("id") Integer id) {
        Platform returnPlatform = platformDAO.getPlatformById(id)
        ResponseBuilder responseBuilder

        if (returnPlatform == null) {
            responseBuilder = notFound()
        }
        else {
            platformDAO.deleteById(id)
            responseBuilder = ok(returnPlatform)
        }
        responseBuilder.build()
    }

    @Path("/{id : \\d+}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putPlatform(@Valid Platform newPlatform, @PathParam("id") Integer id) {
        ResponseBuilder responseBuilder
        Platform returnPlatform

        Platform checkPlatform = platformDAO.getPlatformById(id)

        //POSTs if the platform doesn't already exist
        if (!checkPlatform) {
            responseBuilder = postHelper(newPlatform)
        }
        //Otherwise execute a PUT
        else {
            if (!validReleaseYear(newPlatform.releaseYear)) {
                responseBuilder = badRequest("Invalid release year")
            } else {
                try {
                    platformDAO.putPlatform(id, newPlatform.releaseYear, newPlatform.name, newPlatform.manufacturer, newPlatform.computer, newPlatform.console)
                    returnPlatform = platformDAO.platformByName(newPlatform.name)
                    responseBuilder = ok(returnPlatform)
                } catch (UnableToExecuteStatementException err) {
                    String executeError = err.getMessage()
                    responseBuilder = badRequest(executeError)
                }
            }
        }
        responseBuilder.build()
    }

    private ResponseBuilder postHelper(Platform postPlatform) {
        ResponseBuilder responseBuilder

        if (!validReleaseYear(postPlatform.releaseYear)) {
            responseBuilder = badRequest("Invalid release year")
        } else {
            try {
                platformDAO.postPlatform(postPlatform.releaseYear, postPlatform.name, postPlatform.manufacturer, postPlatform.computer, postPlatform.console)

                //necessary to find and return the new object this way because it comes in w/o an id and dates
                Platform returnPlatform = platformDAO.platformByName(postPlatform.name)

                responseBuilder = ok(returnPlatform)
            } catch (UnableToExecuteStatementException err) {
                String executeError = err.getMessage()
                responseBuilder = badRequest(executeError)
            }
        }
    }

    private Boolean validReleaseYear(Integer year) {
        if (1000 < year && year < 9999) {
            return 1
        } else {
            return 0
        }
    }
}
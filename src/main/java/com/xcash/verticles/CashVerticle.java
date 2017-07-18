package com.xcash.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.xcash.entity.CashTransaction;
import com.xcash.service.CashService;
import com.xcash.service.CashServiceImpl;
import com.xcash.util.Runner;

public class CashVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CashVerticle.class);

	private static final String HOST = "0.0.0.0";
	private static final int PORT = 8082;
	private CashService service = new CashServiceImpl();
	
	  // Convenience method so you can run it in your IDE
	  public static void main(String[] args) {
	    Runner.runExample(CashVerticle.class);
	  }
	
	@Override
	public void start(Future<Void> future) throws Exception {
		Router router = Router.router(vertx);
		// CORS support
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		Set<HttpMethod> allowMethods = new HashSet<>();
		allowMethods.add(HttpMethod.GET);
		allowMethods.add(HttpMethod.POST);
		allowMethods.add(HttpMethod.DELETE);
		allowMethods.add(HttpMethod.PATCH);

		router.route().handler(BodyHandler.create());
		router.route().handler(
				CorsHandler.create("*").allowedHeaders(allowHeaders)
						.allowedMethods(allowMethods));

		// routes
		router.get("/xcash/v1/cash").handler(this::handlePostCash);

		vertx.createHttpServer()
				.requestHandler(router::accept)
				.listen(config().getInteger("http.port", PORT),
						config().getString("http.address", HOST), result -> {
							if (result.succeeded())
								future.complete();
							else
								future.fail(result.cause());
						});
	}
	
	 private void handlePostCash(RoutingContext context) {
		    String storeId = context.request().getParam("storeId");
		    if (storeId == null) {
		      sendError(400, context.response());
		      return;
		    }
		    CashTransaction transaction = new CashTransaction();
		    service.postCash(transaction).setHandler(resultHandler(context, res -> {
		      if (res==null)
		        notFound(context);
		      else {
		        final String encoded = Json.encodePrettily(res);
		        context.response()
		          .putHeader("content-type", "application/json")
		          .end(encoded);
		      }
		    }));
		  }
	 
	  /**
	   * Wrap the result handler with failure handler (503 Service Unavailable)
	   */
	  private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Consumer<T> consumer) {
	    return res -> {
	      if (res.succeeded()) {
	        consumer.accept(res.result());
	      } else {
	        serviceUnavailable(context);
	      }
	    };
	  }
	  
	  private void sendError(int statusCode, HttpServerResponse response) {
	    response.setStatusCode(statusCode).end();
	  }

	  private void notFound(RoutingContext context) {
	    context.response().setStatusCode(404).end();
	  }

	  private void badRequest(RoutingContext context) {
	    context.response().setStatusCode(400).end();
	  }

	  private void serviceUnavailable(RoutingContext context) {
	    context.response().setStatusCode(503).end();
	  }
}

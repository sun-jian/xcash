package com.xcash.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

import com.xcash.service.OcrService;
import com.xcash.util.Runner;

public class UploadVerticle extends AbstractVerticle {
	private OcrService ocrService;
	
	// Convenience method so you can run it in your IDE
	  public static void main(String[] args) {
	    Runner.runExample(UploadVerticle.class);
	  }

	  @Override
	  public void start() throws Exception {
		this.ocrService = new OcrService(vertx);
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
		
		vertx.createHttpServer().requestHandler(req -> {
	      if (req.uri().startsWith("/files")) {
	        req.setExpectMultipart(true);
	        req.uploadHandler(upload -> {
	          upload.exceptionHandler(cause -> {
	            req.response().setChunked(true).end("Upload failed");
	          });
	          String suffix = upload.filename().substring(upload.filename().lastIndexOf("."));
	          
	          String path = "/data/uploads/" + System.currentTimeMillis()+suffix;
	          upload.endHandler(v -> {
	        	  ocrService.recognize(path, res -> {
	        		  HttpServerResponse resp = req.response().setChunked(true);
	        		  success(resp, res.result());
	        	  });
	          });
	          // FIXME - Potential security exploit! In a real system you must check this filename
	          // to make sure you're not saving to a place where you don't want!
	          // Or better still, just use Vert.x-Web which controls the upload area.
	          upload.streamToFileSystem(path);
	          
	        });
	      } else {
	        req.response().setStatusCode(404);
	        req.response().end();
	      }
	    }).listen(8083);

	  }
	  
	  private void success(HttpServerResponse resp, JsonObject response) {
		  resp.putHeader("content-type", "application/json")
		      .putHeader("Access-Control-Allow-Origin", "*")
		      .putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS").end(response.encodePrettily());
	  }
	  
}

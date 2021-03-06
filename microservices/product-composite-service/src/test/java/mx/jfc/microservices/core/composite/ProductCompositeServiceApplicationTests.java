package mx.jfc.microservices.core.composite;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.mockito.Mockito.when;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;


import mx.jfc.microservices.core.api.composite.product.ProductAggregate;
import mx.jfc.microservices.core.api.composite.product.RecommendationSummary;
import mx.jfc.microservices.core.api.composite.product.ReviewSummary;
import mx.jfc.microservices.core.api.core.product.Product;
import mx.jfc.microservices.core.api.core.recommendation.Recommendation;
import mx.jfc.microservices.core.api.core.review.Review;
import mx.jfc.microservices.core.api.exceptions.InvalidInputException;
import mx.jfc.microservices.core.api.exceptions.NotFoundException;
import mx.jfc.microservices.core.composite.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired private WebTestClient webTestClient;
	@MockBean private ProductCompositeIntegration compositeIntegration;

	@BeforeEach
	void setUp(){
		when(compositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK,"name",1,"Mock address"));
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn( Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content","mock address")));
		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK,1,"Author","Subject","Content","Mock address")));

		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT_FOUND " + PRODUCT_ID_NOT_FOUND));
		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID "+PRODUCT_ID_INVALID));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createCompositeProduct1(){
		ProductAggregate compositeProduct = new ProductAggregate(1,"name",1,null,null,null);
		postAndVerifyProduct(compositeProduct, OK);
	}

	@Test
	void createCompositeProduct2(){
		ProductAggregate compositeProduct =  new ProductAggregate(1, "name", 2,
					singletonList(new RecommendationSummary(1,"a",1,"c")),
					singletonList(new ReviewSummary(1,"a","s","c")),null
				);
		postAndVerifyProduct(compositeProduct, OK);
	}

	@Test
	void deleteCompositeProduct(){
		ProductAggregate compositeProduct =  new ProductAggregate(1, "name", 2,
				singletonList(new RecommendationSummary(1,"a",1,"c")),
				singletonList(new ReviewSummary(1,"a","s","c")),null
		);
		postAndVerifyProduct(compositeProduct, OK);
		deleteAndVerifyProduct(compositeProduct.getProductId(),OK);
		deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
	}
	@Test
	void getProductById(){
		getAndVerifyProduct(PRODUCT_ID_OK,OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);

	}

	@Test
	void getProductNotFound(){
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND,NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT_FOUND " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidInput(){
		getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID " + PRODUCT_ID_INVALID);

	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus){
		return webTestClient.get()
				.uri("/product-composite/" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus){
		webTestClient.post()
				.uri("/product-composite")
				.body(just(compositeProduct), ProductAggregate.class)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
		webTestClient.delete()
				.uri("/product-composite/" + productId)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}
}

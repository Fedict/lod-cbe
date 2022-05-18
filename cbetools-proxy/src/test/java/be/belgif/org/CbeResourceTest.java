/*
 * Copyright (c) 2020, FPS BOSA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.belgif.org;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Bart.Hanssens
 */
@QuarkusTest
public class CbeResourceTest {
	@Test
    public void testEndpointOrgNTOld() {
        given()
          .when().accept("application/n-triples").get("/id/cbe/org/0671_516_647")
          .then().statusCode(200);
    }

	@Test
    public void testEndpointOrgNTNew() {
        given()
          .when().accept("application/n-triples").get("/id/CbeRegisteredEntity/0671516647")
          .then().statusCode(200)
			.and()
			.body(containsString("<https://org.belgif.be/id/CbeRegisteredEntity/0671516647> <http://www.w3.org/2004/02/skos/core#altLabel> \"FOD BOSA\"@nl ."));
    }
	
	@Test
    public void testEndpointOrgNTErrorTooShort() {
        given()
          .when().accept("application/n-triples").get("/id/CbeRegisteredEntity/067")
          .then().statusCode(400);
    }

	@Test
    public void testEndpointOrgNTErrorWrong() {
        given()
          .when().accept("application/n-triples").get("/id/CbeRegisteredEntity/ABC")
          .then().statusCode(400);
    }

	@Test
    public void testEndpointOrgNTErrorTooLong() {
        given()
          .when().accept("application/n-triples").get("/id/CbeRegisteredEntity/01234567890123456789")
          .then().statusCode(400);
    }

	@Test
    public void testEndpointOrgJson() {
        given()
          .when().accept("application/ld+json").get("/id/cbe/org/0671_516_647")
          .then().statusCode(200);
    }
	
	@Test
    public void testEndpointSiteNTOld() {
        given()
          .when().accept("application/n-triples").get("/id/cbe/site/2_147_812_701")
          .then().statusCode(200);
    }
	
	@Test
    public void testEndpointSiteNTNew() {
        given()
          .when().accept("application/n-triples").get("/id/CbeEstablishmentUnit/2147812701")
          .then().statusCode(200);
    }

	@Test
    public void testEndpointSiteJsonOld() {
        given()
          .when().accept("application/ld+json").get("/id/cbe/site/2_147_812_701")
          .then().statusCode(200);
    }

	@Test
    public void testEndpointSiteJsonNew() {
        given()
          .when().accept("application/ld+json").get("/id/cbe/site/2147812701")
          .then().statusCode(200);
    }
}

package com.example.demostorage.DemoStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DemoStorageApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private DemoStorageApplication controller;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

	@Test
	public void basicServerEndpointTest() throws Exception {

		String basePath = "http://localhost:" + port + "/";

		// base returns 404
		assertThat(this.restTemplate.getForObject(basePath,
				String.class)).contains("404");

	}

	@Test
	public void missingStorageKeyShouldFailTest() throws Exception {
		// get, put and delete on /storage/ should return 400
		MockHttpServletRequestBuilder builderGet =
				MockMvcRequestBuilders.get("/storage/");
		MockHttpServletRequestBuilder builderPut =
				MockMvcRequestBuilders.put("/storage/")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.accept(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF-8")
						.content("{}");
		MockHttpServletRequestBuilder builderDelete =
				MockMvcRequestBuilders.delete("/storage/");
		this.mockMvc.perform(builderGet)
				.andExpect(MockMvcResultMatchers.status()
						.isBadRequest())
				.andDo(MockMvcResultHandlers.print());
		this.mockMvc.perform(builderPut)
				.andExpect(MockMvcResultMatchers.status()
						.isBadRequest())
				.andDo(MockMvcResultHandlers.print());
		this.mockMvc.perform(builderDelete)
				.andExpect(MockMvcResultMatchers.status()
						.isBadRequest())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void mainStorageEndpointTest() throws Exception {
		/*
		Basic test (available_capacity has to be >= 1)
		- GET test storageItem -> should return 404
		- GET available_capacity -> should return a number >= 0
		- PUT test storageItem, lower than or equal to available_capacity -> should return 200
		- GET test storageItem again -> should return the 200 + data
		- GET available_capacity again -> should return old_capacity - test storageItem capacity
		- DELETE test storageItem -> should return 200
		- GET test storageItem again -> should return 404
		- GET available_capacity again -> should return old_capacity

		 */

		MockHttpServletRequestBuilder builderGetAvailableCapacity =
				MockMvcRequestBuilders.get("/storage/available_capacity")
						.characterEncoding("UTF-8");
		String testStorageItemKey = "test_item4";
		String testStorageItemContentKey = testStorageItemKey + "testKey";
		long testStorageCapacity = 1L;
		MockHttpServletRequestBuilder builderGetTestItem =
				MockMvcRequestBuilders.get("/storage/" + testStorageItemKey)
						.accept(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF-8");
		MockHttpServletRequestBuilder builderPutTestItem =
				MockMvcRequestBuilders.put("/storage/" + testStorageItemKey)
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.accept(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF-8")
						.content("{\"" + testStorageItemContentKey + "\":1}");
		MockHttpServletRequestBuilder builderDeleteTestItem =
				MockMvcRequestBuilders.delete("/storage/" + testStorageItemKey)
						.characterEncoding("UTF-8");

		// available_capacity has to be >= 1
		long availableCapacity = controller.storage.getAvailableCapacity();
		assertThat(availableCapacity).isGreaterThanOrEqualTo(1);


		// GET test storageItem -> should return 404
		this.mockMvc.perform(builderGetTestItem)
				.andExpect(MockMvcResultMatchers.status()
						.isNotFound())
				.andDo(MockMvcResultHandlers.print());


		// GET available_capacity -> should return a number >= 0
		MvcResult result = this.mockMvc.perform(builderGetAvailableCapacity)
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

		long initial_available_capacity = Long.parseLong(result.getResponse().getContentAsString(), 10);
		assertThat(initial_available_capacity).isEqualTo(availableCapacity);


		// PUT test storageItem, lower than or equal to available_capacity -> should return 200
		this.mockMvc.perform(builderPutTestItem)
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andDo(MockMvcResultHandlers.print());


		// GET test storageItem again -> should return the 200 + data
		this.mockMvc.perform(builderGetTestItem)
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string(containsString(testStorageItemContentKey)))
				.andDo(MockMvcResultHandlers.print());


		// GET available_capacity again -> should return old_capacity - test storageItem capacity
		result = this.mockMvc.perform(builderGetAvailableCapacity)
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

		long new_available_capacity = Long.parseLong(result.getResponse().getContentAsString(), 10);
		assertThat(new_available_capacity).isEqualTo(initial_available_capacity - testStorageCapacity);


		// DELETE test storageItem -> should return 200
		this.mockMvc.perform(builderDeleteTestItem)
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andDo(MockMvcResultHandlers.print());


		// GET test storageItem again -> should return 404
		this.mockMvc.perform(builderGetTestItem)
				.andExpect(MockMvcResultMatchers.status()
						.isNotFound())
				.andDo(MockMvcResultHandlers.print());


		// GET available_capacity again -> should return old_capacity
		this.mockMvc.perform(builderGetAvailableCapacity)
				.andExpect(MockMvcResultMatchers.status()
						.isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string(Long.toString(initial_available_capacity)))
				.andDo(MockMvcResultHandlers.print());
	}

}

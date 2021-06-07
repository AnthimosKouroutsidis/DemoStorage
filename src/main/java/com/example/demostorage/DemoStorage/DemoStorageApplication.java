package com.example.demostorage.DemoStorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@SpringBootApplication
@RestController
public class DemoStorageApplication {

	Storage storage = new Storage(5);

	public static void main(String[] args) {
		SpringApplication.run(DemoStorageApplication.class, args);
	}

	@GetMapping("/storage/{key}")
	public ResponseEntity<?> getStorageItem(@PathVariable("key") String key) {
		try {
			return new ResponseEntity<>(storage.getItem(key), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Cannot find storage item with key '" + key + "'", HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping("/storage/{key}")
	public ResponseEntity<?> putStorageItem(@PathVariable("key") String key,
											@RequestBody() HashMap<String, Object> value) {
		try {
			storage.setItem(key, value);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/storage/{key}")
	public ResponseEntity<?> deleteStorageItem(@PathVariable("key") String key) {
		storage.deleteItem(key);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/*
	Get the storage "version" (it is being incremented by 1 with every change)
	 */
	@GetMapping("/storage/version")
	public long getStorageVersion() {
		return storage.getVersion();
	}

	/*
	Get the available capacity (DEMO property)
	 */
	@GetMapping("/storage/available_capacity")
	public long getAvailableStorageCapacity() {
		return storage.getAvailableCapacity();
	}

	/*
	Get a list with all storage items
	 */
	@GetMapping("/storage/list")
	public List<String> getStorageItemList() {
		return storage.getItemList();
	}
}

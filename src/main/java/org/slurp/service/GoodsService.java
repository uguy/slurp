package org.slurp.service;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

@Named("goodsService")
public class GoodsService {

	// use a tree map so they become sorted
	private final Map<String, String> goods = new TreeMap<String, String>();

	private Random ran = new Random();

	private AtomicInteger userSeq = new AtomicInteger();

	public GoodsService() {
		goods.put("123", "Coffee");
		goods.put("456", "Bath Duck");
		goods.put("789", "Californian Turtle");
	}

	/**
	 * Gets a user by the given id
	 *
	 * @param id the id of the user
	 * @return the user, or <tt>null</tt> if no user exists
	 */
	public String get(String id) {
		if ("789".equals(id)) {
			// simulate some cpu processing time when returning the slow turtle
			int delay = 500 + ran.nextInt(1500);
			try {
				Thread.sleep(delay);
			}
			catch (Exception e) {
				// ignore
			}
		}
		return goods.get(id);
	}

	public Collection<String> list() {
		return goods.values();
	}

	public void update(String data) {
		// goods.put("" + data.getId(), data);
	}

	/**
	 * Creates the given user
	 *
	 * @param user the user
	 */
	public void create(String data) {
		int id = userSeq.incrementAndGet();
		goods.put("" + id, data);
	}
}

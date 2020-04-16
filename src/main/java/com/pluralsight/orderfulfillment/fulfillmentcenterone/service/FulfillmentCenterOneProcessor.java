package com.pluralsight.orderfulfillment.fulfillmentcenterone.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.pluralsight.orderfulfillment.generated.OrderItemType;
import com.pluralsight.orderfulfillment.generated.OrderType;

@Component
public class FulfillmentCenterOneProcessor {
  private static final Logger log = LoggerFactory.getLogger(FulfillmentCenterOneProcessor.class);
  
  public String transformToOrderRequestMessage(String orderXml) {
	  String output = null;
	  try {
		  if(orderXml == null) {
			  throw new Exception("Order XML was not bound to the method via integration framnework.");
		  }
		  output = processCreateOrderRequestMessage(orderXml);
	  }catch(Exception e) {
		  log.error("Fulfillment center one message translation failed: " 
	      + e.getMessage(), e);
	  }
	  return output;
  }
  
  protected String processCreateOrderRequestMessage(String orderXml) throws Exception {
	  // 1. Un-marshal the order from an XML string to the generated order
	  JAXBContext context = JAXBContext.newInstance(com.pluralsight.orderfulfillment.generated.Order.class);
	  Unmarshaller unmarshaller = context.createUnmarshaller();
	  com.pluralsight.orderfulfillment.generated.Order order = 
			  (com.pluralsight.orderfulfillment.generated.Order) unmarshaller
			  .unmarshal(new StringReader(orderXml));
	  // 2. Build an Order Request object and return its JSON representation
	  return new Gson().toJson(buildOrderRequestType(order));
  }
  
  protected OrderRequest buildOrderRequestType(com.pluralsight.orderfulfillment.generated.Order orderFromXml) {
	  OrderType orderTypeFromXml = orderFromXml.getOrderType();
	  // 1. Build order item types
	  List<OrderItemType> orderItemTypesFromXml = orderTypeFromXml.getOrderItems();
	  List<OrderItem> orderItems = new ArrayList<OrderItem>();
	  for (OrderItemType orderItemTypeFromXml: orderItemTypesFromXml) {
		  orderItems.add(new OrderItem(orderItemTypeFromXml.getItemNumber(),
				  orderItemTypeFromXml.getPrice(), orderItemTypeFromXml.getQuantity()));
	  }
	  
	  // 2. Build order
	  List<Order> orders = new ArrayList<Order>();
	  Order order = new Order();
	  order.setFirstName(orderTypeFromXml.getFirstName());
	  order.setLastName(orderTypeFromXml.getLastName());
	  order.setEmail(orderTypeFromXml.getEmail());
	  order.setOrderNumber(orderTypeFromXml.getOrderNumber());
	  order.setTimeOrderPlaced(orderTypeFromXml.getTimeOrderPlaced().toGregorianCalendar().getTime());
	  order.setOrderItems(orderItems);
	  orders.add(order);
	  
	  // 3. Build Order Request
	  OrderRequest orderRequest = new OrderRequest();
	  orderRequest.setOrders(orders);
      // 4. Return the order request
	  return orderRequest;
  }
}

import Order from "../models/order.model.js";
import User from "../models/user.model.js";

// Place a New Order
export const takeOrder = async (req, res) => {
  try {
    const { username, products, totalPrice, address } = req.body;

    if (!username || !products || !totalPrice) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    const userId = await User.findOne({ username: username }).select("_id");

    const newOrder = new Order({
      userId,
      products,
      totalPrice,
      address,
    });

    await newOrder.save();
    res
      .status(201)
      .json({ message: "Order placed successfully", order: newOrder });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// Get All Orders (For Admin Panel)
export const getAllOrders = async (req, res) => {
  try {
    const orders = await Order.find().populate("userId", "username email");
    res.json(orders);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// Get Order Details (For Viewing a Specific Order)
export const getOrderDetails = async (req, res) => {
  try {
    const { orderId } = req.params;
    const order = await Order.findById(orderId).populate(
      "userId",
      "username email"
    );

    if (!order) {
      return res.status(404).json({ message: "Order not found" });
    }

    res.json({
      order,
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// Update Order Status (Admin Can Change Status)
export const updateOrderStatus = async (req, res) => {
  try {
    const { orderId } = req.params;
    const { orderStatus } = req.body;

    // Validate the order status
    if (!["pending", "delivered", "cancelled"].includes(orderStatus)) {
      return res.status(400).json({ message: "Invalid status" });
    }

    // Update the order status
    const updatedOrder = await Order.findByIdAndUpdate(
      orderId,
      { orderStatus },
      { new: true }
    );

    // Check if the order was found and updated
    if (!updatedOrder) {
      return res.status(404).json({ message: "Order not found" });
    }

    // Return success response
    res.json({
      message: "Order status updated successfully",
      order: updatedOrder,
    });
  } catch (error) {
    // Handle unexpected errors
    console.error("Error updating order status:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};

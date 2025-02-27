import Order from "../models/order.model.js";

// Place a New Order
export const takeOrder = async (req, res) => {
  try {
    const { userId, products, totalPrice } = req.body;

    if (!userId || !products || !totalPrice) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    const newOrder = new Order({
      userId,
      products,
      totalPrice,
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
    const order = await Order.findById(orderId);

    if (!order) {
      return res.status(404).json({ message: "Order not found" });
    }

    const totalOrders = await Order.countDocuments({ userId: order.userId });
    const confirmedOrders = await Order.countDocuments({
      userId: order.userId,
      orderStatus: "delivered",
    });
    const cancelledOrders = await Order.countDocuments({
      userId: order.userId,
      orderStatus: "cancelled",
    });
    const pendingOrders = await Order.countDocuments({
      userId: order.userId,
      orderStatus: "pending",
    });

    res.json({
      order,
      totalOrders,
      confirmedOrders,
      cancelledOrders,
      pendingOrders,
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

    if (!["pending", "delivered", "cancelled"].includes(orderStatus)) {
      return res.status(400).json({ message: "Invalid status" });
    }

    const updatedOrder = await Order.findByIdAndUpdate(
      orderId,
      { orderStatus },
      { new: true }
    );

    if (!updatedOrder) {
      return res.status(404).json({ message: "Order not found" });
    }

    res.json({
      message: "Order status updated successfully",
      order: updatedOrder,
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

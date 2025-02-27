import mongoose from "mongoose";

const orderSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
  products: [
    {
      title: String,
      image: String,
      price: Number,
      quantity: Number,
    },
  ],
  totalPrice: Number,
  orderStatus: {
    type: String,
    enum: ["pending", "delivered", "cancelled"],
    default: "pending",
  },
  //orderDate: { type: Date, default: Date.now },
  createdAt: { type: Date, default: Date.now },
});

const Order = mongoose.model("Order", orderSchema);
export default Order;

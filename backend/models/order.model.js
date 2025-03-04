import mongoose from "mongoose";

const orderSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    products: [
      {
        title: String,
        image: String,
        price: Number,
        quantity: Number,
      },
    ],
    address: [
      {
        city: String,
        country: String,
        address: String,
        postalCode: String,
        phoneNumber: String,
      },
    ],
    totalPrice: Number,
    orderStatus: {
      type: String,
      enum: ["pending", "delivered", "cancelled"],
      default: "pending",
    },
  },
  {
    timestamps: true,
  }
);

const Order = mongoose.model("Order", orderSchema);
export default Order;

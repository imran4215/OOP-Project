import mongoose from "mongoose";

const productSchema = new mongoose.Schema({
  query: String,
  data: Object,
  savedAt: { type: Date, default: Date.now },
});

const Product = mongoose.model("Product", productSchema);
export default Product;

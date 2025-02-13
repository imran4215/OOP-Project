import express from "express";
import {
  getAllProducts,
  getProductsBySearch,
  saveAllProducts,
} from "../controllers/product.controller.js";

const router = express.Router();

router.get("/saveProducts", saveAllProducts);
router.get("/getAllProducts", getAllProducts);
router.get("/search", getProductsBySearch);

export default router;

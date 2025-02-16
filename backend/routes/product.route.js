import express from "express";
import {
  demo,
  getProducts,
  getProductsBySearch,
  saveProducts,
} from "../controllers/product.controller.js";

const router = express.Router();

router.get("/saveProducts", saveProducts);
router.get("/getProducts", getProducts);
router.get("/search", getProductsBySearch);
router.get("/demo", demo);

export default router;

import expressc from "express";
import {
  getAllOrders,
  getOrderDetails,
  takeOrder,
  updateOrderStatus,
} from "../controllers/order.controller.js";

const router = expressc.Router();

router.post("/takeOrder", takeOrder);
router.get("/getAllOrders", getAllOrders);
router.post("/getOrderDetails/:orderId", getOrderDetails);
router.post("/updateOrderStatus/:orderId", updateOrderStatus);

export default router;

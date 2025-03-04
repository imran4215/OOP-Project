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
router.get("/getOrderDetails/:orderId", getOrderDetails);
router.put("/updateOrderStatus/:orderId", updateOrderStatus);

export default router;

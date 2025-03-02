import express from "express";
import {
  deleteUser,
  login,
  signup,
  getAllUsers,
  userDetails,
} from "../controllers/user.controller.js";

const router = express.Router();

router.post("/signup", signup);
router.post("/login", login);
router.delete("/deleteUser/:username", deleteUser);
router.get("/allUsers", getAllUsers);
router.post("/userDetails", userDetails);

export default router;

import bcrypt from "bcrypt";
import User from "../models/user.model.js";
import { generateToken } from "../lib/utils.js";
import Order from "../models/order.model.js";

export const signup = async (req, res) => {
  try {
    const { username, email, password, confirmPassword } = req.body;

    //Check if all fields are filled
    if (!username || !email || !password || !confirmPassword) {
      return res.status(400).json({ error: "All fields are required" });
    }

    //Check if password and confirmPassword are same
    if (password !== confirmPassword) {
      return res.status(400).json({ error: "Passwords do not match" });
    }

    //Check if user already exists or not
    const user = await User.findOne({ $or: [{ email }, { username }] });
    if (user) {
      if (user.email === email) {
        return res.status(400).json({ error: "Email already exists" });
      }
      return res.status(400).json({ error: "Username already exists" });
    }

    //Hash password
    const hashPassword = await bcrypt.hash(password, 10);

    //Create new user & save to database
    const newUser = new User({
      username,
      email,
      password: hashPassword,
    });
    await newUser.save();

    //Send success response
    res.status(201).json({ message: "User registered successfully", newUser });
  } catch (error) {
    console.error("Error occuring in signing up", error.message);
  }
};

export const login = async (req, res) => {
  try {
    const { email, password } = req.body;

    //Check if all fields are filled
    if (!email || !password) {
      return res.status(400).json({ error: "All fields are required" });
    }

    //Find user in database
    const user = await User.findOne({ email });

    //Check if password is correct
    const isPasswordValid = await bcrypt.compare(password, user.password);

    //Check if user exists or not
    if (!user || !isPasswordValid) {
      return res.status(400).json({ error: "Invalid username or password" });
    }

    //Send success response
    if (user) {
      generateToken(user._id, res);
      res.status(200).json({ message: "User logged in successfully", user });
    } else {
      res.status(500).json({ error: "Internal server error" });
    }
  } catch (error) {
    console.error("Error occuring in login", error.message);
  }
};

export const deleteUser = async (req, res) => {
  try {
    const { username } = req.params;

    //Check if username is provided
    if (!username) {
      return res.status(400).json({ error: "Username is required" });
    }

    //Find user in database
    const user = await User.findOne({ username });

    //Check if user exists or not
    if (!user) {
      return res.status(400).json({ error: "User does not exist" });
    }

    //Delete user from database
    await User.deleteOne({ username });

    //Send success response
    res.status(200).json({ message: "User deleted successfully", user });
  } catch (error) {
    console.error("Error occuring in deleting user", error.message);
  }
};

export const getAllUsers = async (req, res) => {
  try {
    // Fetch all users
    const users = await User.find();

    // For each user, calculate their total, pending, delivered, and canceled orders
    const usersWithOrderStats = [];

    for (let user of users) {
      // Get order counts based on statuses for each user
      const totalOrders = await Order.countDocuments({ userId: user._id });
      const pendingOrders = await Order.countDocuments({
        userId: user._id,
        orderStatus: "pending",
      });
      const deliveredOrders = await Order.countDocuments({
        userId: user._id,
        orderStatus: "delivered",
      });
      const canceledOrders = await Order.countDocuments({
        userId: user._id,
        orderStatus: "cancelled",
      });

      // Push user with their order stats to the result array
      usersWithOrderStats.push({
        ...user._doc, // Spread the user's data
        totalOrders,
        pendingOrders,
        deliveredOrders,
        canceledOrders,
      });
    }

    // Send success response
    res.status(200).json({
      message: "All users with order stats fetched successfully",
      users: usersWithOrderStats,
    });
  } catch (error) {
    console.error(
      "Error occurred in fetching all users with order stats",
      error.message
    );
    res.status(500).json({ message: "Internal server error" });
  }
};

export const userDetails = async (req, res) => {
  try {
    const { username } = req.params;

    //Check if username is provided
    if (!username) {
      return res.status(400).json({ error: "Username is required" });
    }

    //Find user in database
    const user = await User.findOne({ username });

    //Check if user exists or not
    if (!user) {
      return res.status(400).json({ error: "User does not exist" });
    }

    const totalOrders = await Order.countDocuments({ userId: user._id });
    const pendingOrders = await Order.countDocuments({
      userId: user._id,
      orderStatus: "pending",
    });
    const deliveredOrders = await Order.countDocuments({
      userId: user._id,
      orderStatus: "delivered",
    });
    const canceledOrders = await Order.countDocuments({
      userId: user._id,
      orderStatus: "cancelled",
    });

    const orders = await Order.find({ userId: user._id });
    //Send success response
    res.status(200).json({
      message: "User details fetched successfully",
      user,
      totalOrders,
      pendingOrders,
      deliveredOrders,
      canceledOrders,
      orders,
    });
  } catch (error) {
    console.error("Error occuring in fetching user details", error.message);
  }
};

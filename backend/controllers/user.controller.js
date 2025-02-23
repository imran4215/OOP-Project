import bcrypt from "bcrypt";

import User from "../models/user.model.js";
import { generateToken } from "../lib/utils.js";

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

export const logout = async (req, res) => {
  try {
    res.clearCookie("jwt");
    res.status(200).json({ message: "User logged out successfully" });
  } catch (error) {
    console.error("Error occuring in logout", error.message);
  }
};

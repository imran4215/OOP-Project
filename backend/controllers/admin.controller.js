import Admin from "../models/admin.model.js";

export const adminSignup = async (req, res) => {
  try {
    const { email, password } = req.body;
    const admin = await Admin.findOne({
      email,
    });
    if (admin) {
      res.status(400).json({ message: "Admin already exists" });
    } else {
      const newAdmin = new Admin({
        email,
        password,
      });
      await newAdmin.save();
      res.status(201).json({ message: "Admin created successfully", newAdmin });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

export const adminLogin = async (req, res) => {
  try {
    const { email, password } = req.body;
    const admin = await Admin.findOne({ email, password });
    if (admin) {
      res.status(200).json({ message: "Admin logged in successfully", admin });
    } else {
      res.status(404).json({ message: "Admin not found" });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

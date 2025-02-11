//Package imports
import express from "express";
import dotenv from "dotenv";
import cors from "cors";
import cookieParser from "cookie-parser";

//File imports
import userRoute from "./routes/user.route.js";
import { connectDB } from "./lib/db.js";

//Configurations
dotenv.config();

//Variables
const app = express();
const PORT = process.env.PORT || 3000;

//Middlewares
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(cors());

//Routes
app.use("/api/user", userRoute);

//Server
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
  connectDB(); //connect to database
});

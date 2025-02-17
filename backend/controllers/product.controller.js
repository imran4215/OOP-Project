import axios from "axios";
import Product from "../models/product.model.js";
import { scrapeAndSort } from "../scrapers/scrapeAndSort.js";
import { demoScrape } from "../scrapers/demoScrape.js";

export const saveProducts = async (req, res) => {
  try {
    const { data } = await axios.get(
      "https://fakestoreapi.com/products?limit=3"
    );

    data.map(async (product) => {
      const newProduct = new Product({
        productName: product.title,
        productDetails: product.description,
        productPrice: product.price,
        productImage: product.image,
        category: product.category,
      });
      await newProduct.save();
    });

    res.status(200).json(data);
  } catch (error) {
    res.status(404).json({ message: error.message });
  }
};

export const getProducts = async (req, res) => {
  try {
    const products = await Product.find();
    res.status(200).json(products);
  } catch (error) {
    res.status(404).json({ message: error.message });
  }
};

export const getProductsBySearch = async (req, res) => {
  const { query } = req.query;

  if (!query) {
    return res.status(400).json({ error: "Search query is required" });
  }

  try {
    const results = await scrapeAndSort(query);
    res.send(results);
  } catch (error) {
    res.status(500).json({ error: "Error fetching products" });
  }
};

export const demo = async (req, res) => {
  const { query } = req.query;

  if (!query) {
    return res.status(400).json({ error: "Search query is required" });
  }

  try {
    const htmlData = await demoScrape(query);
    // res.json({
    //   message: "Query parameters received",
    //   data: htmlData,
    // });
    res.send(htmlData);
  } catch (error) {
    res.status(500).json({ error: "Error fetching products" });
  }
};

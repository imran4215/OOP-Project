import Product from "../models/product.model.js";
import { scrapeAndSort } from "../scrapers/scrapeAndSort.js";
import { demoScrape } from "../scrapers/demoScrape.js";

export const saveProducts = async (req, res) => {
  const { query } = req.query;

  try {
    const results = await scrapeAndSort(query);

    const newProducts = new Product({
      query,
      data: results,
    });

    await newProducts.save();

    res
      .status(201)
      .json({ message: "Scraped data saved successfully!", newProducts });
  } catch (error) {
    console.error("Error saving scraped data:", error.message);
    res
      .status(500)
      .json({ error: "Failed to save scraped data", details: error.message });
  }
};

export const getProducts = async (req, res) => {
  const { query } = req.query;

  try {
    const products = await Product.findOne({ query });

    if (!products) {
      return res
        .status(404)
        .json({ message: "No products found for this query." });
    }

    res
      .status(200)
      .json({ message: "Products fetched successfully", products });
  } catch (error) {
    res
      .status(500)
      .json({ message: "Failed to fetch products", error: error.message });
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

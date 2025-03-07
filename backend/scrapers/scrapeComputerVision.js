import axios from "axios";
// import cheerio from "cheerio";
import * as cheerio from "cheerio";

async function scrapeComputerVision(query) {
  const url = `https://computervision.com.bd/index.php?route=product/search&search=${query}&description=true`;

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data); // Load the HTML data with Cheerio

    // Extract product details from the search results
    const products = [];
    $(".product-thumb").each((index, element) => {
      const productName = $(element).find(".name a").text().trim();
      const productDetails = $(element).find(".name a").attr("href");
      const productPrice = $(element)
        .find(".price-normal")
        .text()
        .trim()
        .replace(/[^\d,\.]/g, "") // Remove non-numeric characters except commas and periods
        .replace(/\.0$/, ""); // Remove trailing `.0` from prices like `1000.0`

      const productImage = $(element)
        .find(".product-img .img-first")
        .attr("src");

      products.push({
        source: "Computer Vision",
        productName,
        productPrice,
        productDetails,
        productImage,
      });

      // Returning `false` breaks out of `.each` loop after 3 products
      // if (products.length === 5) return false;
    });

    return products;
  } catch (error) {
    console.error(`Error scraping Computer Vision: ${error.message}`);
    return [];
  }
}

export { scrapeComputerVision };

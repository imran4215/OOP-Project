import axios from "axios";
// import cheerio from "cheerio";
import * as cheerio from "cheerio";

async function scrapeRyans(query) {
  const url = `https://www.ryans.com/search?q=${query}`;

  try {
    const { data } = await axios.get(url);

    const $ = cheerio.load(data); // Load the HTML data with Cheerio

    // Extract product details from the search results
    const products = [];
    $(".category-single-product").each((index, element) => {
      const productName = $(element)
        .find(".card img")
        .attr("alt")
        .split("#")[0]
        .trim();
      const productDetails = $(element).find(".card a").attr("href");
      const productPrice = $(element)
        .find(".card .pr-text")
        .text()
        .replace(/[^\d,]/g, "")
        .trim();
      if (productPrice === "0") return true;

      const productImage = $(element).find(".card img").attr("src");

      products.push({
        source: "Ryans",
        productName,
        productPrice,
        productDetails,
        productImage,
      });

      // Returning `false` breaks out of `.each` loop after 3 products
      if (products.length === 7) return false;
    });

    return products;
  } catch (error) {
    console.error(`Error scraping Ryans: ${error.message}`);
    return [];
  }
}

export { scrapeRyans };

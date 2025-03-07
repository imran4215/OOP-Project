import axios from "axios";
import * as cheerio from "cheerio";

async function scrapeSellTech(query) {
  const url = `https://selltech.com.bd/index.php?route=product/search&search=${query}`;

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data);

    const products = [];
    $(".product-thumb").each((index, element) => {
      const productName = $(element).find(".name a").text().trim();
      const productDetails = $(element).find(".name a").attr("href");
      const productPrice = $(element)
        .find(".caption .price .price-new")
        .text()
        .replace(/[^\d,]/g, "")
        .trim();
      let productImage =
        $(element).find(".product-img img").attr("srcset") || "";
      if (productImage) {
        productImage = productImage.split(",").pop().trim().split(" ")[0];
      } else {
        productImage =
          $(element).find(".product-img img").attr("data-src") || "";
      }

      products.push({
        source: "Selltech",
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
    console.error("Error fetching and parsing data:", error.message);
    return null;
  }
}

export { scrapeSellTech };

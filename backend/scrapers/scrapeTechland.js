import axios from "axios";
import cheerio from "cheerio";

async function scrapeTechland(query) {
  const url = `https://www.techlandbd.com/index.php?route=product/search&search=${query}`;
  const products = [];

  try {
    const { data } = await axios.get(url);
    const $ = cheerio.load(data);

    $(".product-item").each((_, element) => {
      const name = $(element).find(".product-title a").text().trim();
      const price = parseFloat(
        $(element)
          .find(".price")
          .text()
          .replace(/[^0-9.]/g, "")
      );
      const link = $(element).find(".product-title a").attr("href");

      products.push({
        source: "Techland",
        name,
        price,
        link: `https://www.techlandbd.com${link}`,
      });
    });

    return products;
  } catch (error) {
    console.error(`Error scraping Techland: ${error.message}`);
    return [];
  }
}

module.exports = { scrapeTechland };

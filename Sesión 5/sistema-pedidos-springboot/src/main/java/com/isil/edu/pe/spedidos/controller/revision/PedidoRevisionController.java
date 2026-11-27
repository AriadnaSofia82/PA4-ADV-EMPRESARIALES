package com.isil.edu.pe.spedidos.controller.revision;
import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionRequest;
import com.isil.edu.pe.spedidos.exception.PedidoRevisionException;
import com.isil.edu.pe.spedidos.service.revision.PedidoRevisionService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/revision")
public class PedidoRevisionController {

    private final PedidoRevisionService pedidoRevisionService;

    public PedidoRevisionController(
            PedidoRevisionService pedidoRevisionService
    ) {
        this.pedidoRevisionService = pedidoRevisionService;
    }

    /**
     * Muestra los pedidos pendientes de revisión.
     */
    @GetMapping
    public String mostrarRevision(Model model) {

        model.addAttribute(
                "pedidosPendientes",
                pedidoRevisionService.listarPedidosPendientes()
        );

        if (!model.containsAttribute("pedidoRevisionRequest")) {
            model.addAttribute(
                    "pedidoRevisionRequest",
                    new PedidoRevisionRequest()
            );
        }

        return "revision-pedidos";
    }

    /**
     * Registra una revisión y redirige a la pantalla principal.
     */
    @PostMapping
    public String registrarRevision(
            @Valid @ModelAttribute("pedidoRevisionRequest")
            PedidoRevisionRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            String mensaje = bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "Verifique los datos ingresados.";

            redirectAttributes.addFlashAttribute("error", mensaje);

            return "redirect:/revision";
        }

        try {

            pedidoRevisionService.registrarRevision(request);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "La revisión del pedido #" + request.getPedidoId()
                            + " se registró correctamente."
            );

        } catch (PedidoRevisionException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/revision";
    }
}
